'use strict';

const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp(functions.config().firebase);
const cors = require('cors')({origin: true});
const request = require('request');
const express = require('express');

const app = express();

const authenticate = (req, res, next) => {
    console.log("url:", req.url);
    next();
};

app.use(authenticate);
app.use(cors);

// GET /api/coins?type={type}
// Get all coins, optionally specifying a type to filter on
app.get('/coins', (req, res) => {
  const type = req.query.type;
  var filter = 0;
  if (type && ['arbitrage'].indexOf(type) > -1) {
     filter = 1;
  }

  admin.database().ref(`/master/coins`)
  .orderByChild("order")
  .once('value')
  .then(snapshot => {
    var value = snapshot.val();
    if (value) {
        var messages = [];
        snapshot.forEach(childSnapshot => {
            if(filter == 1){
              if(!childSnapshot.hasChild(type)){
                return;
              }
            }
            messages.push({code: childSnapshot.key, name: childSnapshot.val().name, order: childSnapshot.val().order});
        });
        return res.status(200).json({coins: messages});
    } else {
        res.status(401).json({error: 'No data found'});
    }
  }).catch(error => {
    console.log('Error getting messages', error.message);
    res.sendStatus(500);
  });
});

// GET /api/currencies?type={type}
// Get all currencies, optionally specifying a type to filter on
app.get('/currencies', (req, res) => {
  const type = req.query.type;
  let ref = admin.database().ref(`/master/currency`);
  let query = ref.orderByChild('order');
  var filter = 0;
  if (type && ['arbitrage_from', 'arbitrage_to'].indexOf(type) > -1) {
     filter = 1;
     if(type == 'arbitrage_to'){
        query = ref.orderByChild('order_arbitrage_to');
     }
  }

  query
  .once('value')
  .then(snapshot => {
    var value = snapshot.val();
    if (value) {
        var messages = [];
        snapshot.forEach(childSnapshot => {
            if(filter == 1){
              if(!childSnapshot.hasChild(type)){
                return;
              }
            }
            messages.push({code: childSnapshot.key, name: childSnapshot.val().name, order: childSnapshot.val().order});
        });
        return res.status(200).json({currencies: messages});
    } else {
        res.status(401).json({error: 'No data found'});
    }
  }).catch(error => {
    console.log('Error getting messages', error.message);
    res.sendStatus(500);
  });
});

// GET /api/coins_list
// Get all coins list
app.get('/coins_list', (req, res) => {

  admin.database().ref(`/master/coins_list`)
  .orderByChild("order")
  .once('value')
  .then(snapshot => {
    var value = snapshot.val();
    if (value) {
      var messages = [];
      snapshot.forEach(childSnapshot => {
        messages.push({code: childSnapshot.key});
      });
      return res.status(200).json({coins_list: messages});
    } else {
        res.status(401).json({error: 'No data found'});
    }
  }).catch(error => {
    console.log('Error getting messages', error.message);
    res.sendStatus(500);
  });
});

// GET /api/coins_ignore
// Get all coins ignore
app.get('/coins_ignore', (req, res) => {

  admin.database().ref(`/master/coins_ignore`)
  .once('value')
  .then(snapshot => {
    var value = snapshot.val();
    if (value) {
      var messages = [];
      snapshot.forEach(childSnapshot => {
        messages.push({code: childSnapshot.key});
      });
      return res.status(200).json({coins_list: messages});
    } else {
        res.status(401).json({error: 'No data found'});
    }
  }).catch(error => {
    console.log('Error getting messages', error.message);
    res.sendStatus(500);
  });
});


// GET /api/symbols
// Get symbol of all currency codes
app.get('/symbols', (req, res) => {

  admin.database().ref(`/master/symbols`)
  .once('value')
  .then(snapshot => {
    var value = snapshot.val();
    if (value) {
      var messages = [];
      snapshot.forEach(childSnapshot => {
        messages.push({code: childSnapshot.key, symbol: childSnapshot.val()});
      });
      return res.status(200).json({symbols: messages});
    } else {
        res.status(401).json({error: 'No data found'});
    }
  }).catch(error => {
    console.log('Error getting messages', error.message);
    res.sendStatus(500);
  });
});

// GET /api/crons/alert_price
// Triggers price alert check
app.get('/crons/alerts/price', (req, res) => {
  admin.database().ref(`/crons/alerts/price`).set(admin.database.ServerValue.TIMESTAMP);
  return res.status(200).json({alerts: 'triggered'});

  // const promises = [];
  // admin.database().ref(`/alerts/price`).once('value', function(alertSnapshot) {
  //   alertSnapshot.forEach(function(dataSnapshot) {
  //     promises.push(createPriceAlertPromise(dataSnapshot));
  //   });
  // });
  // return Promise.all(promises).then(results => {
  //     return res.status(200).json({alerts: 'triggered'});
  // });
});

// Expose the API as a function
exports.api = functions.https.onRequest(app);

// Checks price alerts for users
exports.priceAlertCheck = functions.database.ref('/crons/alerts/price').onWrite(event => {
  const promises = [];
  admin.database().ref(`/alerts/price`).once('value', function(alertSnapshot) {
    alertSnapshot.forEach(function(dataSnapshot) {
      promises.push(createPriceAlertPromise(dataSnapshot));
    });
  });
  return Promise.all(promises);
});

function createPriceUrl(fromCurrency, toCurrency, exchange) {
  return 'https://min-api.cryptocompare.com/data/price?fsym='
          +fromCurrency+'&tsyms='+toCurrency+(exchange ? '&e='+exchange : '');
}

function createPriceAlertPromise(snapshot) {
  const comboKeyArray = snapshot.key.split('-');
  const fromCurrency = comboKeyArray[0];
  const toCurrency = comboKeyArray[1];
  const exchange = comboKeyArray[2];
  return request(createPriceUrl(fromCurrency, toCurrency, exchange), function (error, response, body) {
      if (!error && response.statusCode == 200) {
        const jsonobj = JSON.parse(response.body);
        const currentPrice = jsonobj[toCurrency];
        const promises = [];

        snapshot.forEach(function(data) {
            promises.push(sendAlertNotifications(snapshot.key, data.key, currentPrice));
        });
        return Promise.all(promises);
      } else {
        console.log('Error fetching price', snapshot.key);
      }
  });
}

function sendAlertNotifications(comboKey, userId, currentPrice) {
  const getUserInstanceIdPromise = admin.database()
                          .ref(`/users/${userId}/instanceId`)
                          .once('value');
  const getUserPriceAlertsPromise = admin.database()
                          .ref(`/user_alerts/prices/${userId}`)
                          .orderByChild('nameStatusIndex')
                          .equalTo(comboKey+'1')
                          .once('value');
  return Promise.all([getUserInstanceIdPromise, getUserPriceAlertsPromise]).then(results => {
    const instanceId = results[0].val();
    const priceAlertSnapshot = results[1];
    // Check if there are any device tokens.
    if (!priceAlertSnapshot.hasChildren()) {
      return console.log('There are no alerts to send for', comboKey);
    }
    console.log("Alerts of users fetched for ", comboKey, " : ", priceAlertSnapshot.numChildren());
    const promises = [];
    priceAlertSnapshot.forEach(function(dataSnapshot) {
        promises.push(sendAlertNotification(instanceId, currentPrice, dataSnapshot));
    });
    return Promise.all(promises);
  });
}

function sendAlertNotification(instanceId, currentPrice, dataSnapshot) {
  const comboKey = dataSnapshot.val().name;
  const comboKeyArray = comboKey.split('-');
  const fromCurrency = comboKeyArray[0];
  const toCurrency = comboKeyArray[1];
  const exchange = comboKeyArray[2];
  const alertPrice = dataSnapshot.val().value;
  const condition = dataSnapshot.val().condition;
  const toSymbol = dataSnapshot.val().toSymbol;
  const frequency = dataSnapshot.val().frequency;
  if(priceAlertConditionCheck(currentPrice, dataSnapshot)) {
    if(frequency == 'onetime'){
      dataSnapshot.ref.update({status: 0, nameStatusIndex: comboKey+"0"});
    }
    // Notification details.
    const payload = {
      notification: {
        title: `${fromCurrency} Price Alert`,
        body: getPriceAlertBody(currentPrice, alertPrice, toSymbol, condition, exchange),
        sound: 'default',
        tag: comboKey
      },
      data: {
        title: `${fromCurrency} Price Alert`,
        body: getPriceAlertBody(currentPrice, alertPrice, toSymbol, condition, exchange),
        name: comboKey,
        type: "alert"
      }
    };
    // Set the message as high priority and have it expire after 24 hours.
    var options = {
      priority: "high",
      timeToLive: 60 * 10
    };

    return admin.messaging().sendToDevice(instanceId, payload, options).then(response => {
      response.results.forEach((result, index) => {
        const error = result.error;
        if (error) {
          console.error('Failure sending message', tokens[index], error);
        }
        console.log("Successfully sent message:", response);
      });
    })
    .catch(error => {
      console.log("Error sending message:", error);
    });
  }
  return;
}

function getPriceAlertBody(currentPrice, alertPrice, toSymbol, condition, exchange) {
  return toSymbol + currentPrice + " (" + getPriceDiff(currentPrice, alertPrice)
          + "% " + getConditionSymbol(condition) + ")"
          + (exchange ? " on "+exchange : "");
}

function priceAlertConditionCheck(currentPrice, dataSnapshot) {
  var result = false;
  const alertPrice = dataSnapshot.val().value;
  const condition = dataSnapshot.val().condition;

  switch (condition) {
      case "<":
          result = currentPrice < alertPrice;
          break;
      case ">":
          result = currentPrice > alertPrice;
          break;
  }
  return result;
}

function getConditionSymbol(condition) {
  var symbol = "";
  switch (condition) {
      case "<":
          symbol = "▼";
          break;
      case ">":
          symbol = "▲";
          break;
  }
  return symbol;
}

function getPriceDiff(currentPrice, alertPrice) {
  var diff = Math.abs((currentPrice - alertPrice)/alertPrice);
  return round(diff*100, 2);
}

function round(value, decimals) {
  return Number(Math.round(value+'e'+decimals)+'e-'+decimals);
}

exports.updateUserToPriceAlert = functions.database.ref('/user_alerts/prices/{uid}/{alertId}').onWrite(event => {
  const snapshot = event.data;
  const uid = event.params.uid;

  // If /user_alert/price was deleted... delete /alert/price aswell
  if (!snapshot.exists()) {
    return;
  }

  const comboKey = snapshot.current.val().name;

  // Only add into /alerts/price if its created for the first time
  if (snapshot.previous.val()) {
    console.log("previous alert exist", comboKey);
    return;
  }

  console.log("added alert", comboKey);
  return admin.database().ref(`/alerts/price/${comboKey}/${uid}`).set(true);
});
