'use strict';

const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp(functions.config().firebase);
const cors = require('cors')({origin: true});
const request = require('request');
const express = require('express');

const app = express();

const authenticate = (req, res, next) => {
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
});

// Expose the API as a function
exports.api = functions.https.onRequest(app);

exports.priceAlertCheck = functions.database.ref('/crons/alerts/price').onWrite(event => {
  const promises = [];
  admin.database().ref(`/alerts/price`).once('value', function(alertSnapshot) {
    alertSnapshot.forEach(function(fromCurrencySnapshot) {
      fromCurrencySnapshot.forEach(function(toCurrencySnapshot) {
        promises.push(createPriceAlertPromise(fromCurrencySnapshot.key, toCurrencySnapshot));
      });
    });
  });
  return Promise.all(promises);
});

function createPriceUrl(fromCurrency, toCurrency) {
  return 'https://min-api.cryptocompare.com/data/price?fsym='
          +fromCurrency+'&tsyms='+toCurrency;
}

function createPriceAlertPromise(fromCurrency, snapshot) {
  const toCurrency = snapshot.key;
  return request(createPriceUrl(fromCurrency, toCurrency), function (error, response, body) {
      if (!error && response.statusCode == 200) {
        const jsonobj = JSON.parse(response.body);
        const currentPrice = jsonobj[toCurrency];
        var tokens = [];
        snapshot.forEach(function(data) {
          const alertPrice = data.val().value;
          const instanceid = data.val().instanceId;
          if(currentPrice > alertPrice) {
            tokens.push(instanceid);
          }
        });
        return sendAlertNotification(tokens, fromCurrency, currentPrice);
      }
  });
}

function sendAlertNotification(tokens, fromCurrency, currentPrice) {
  if(tokens.length == 0){
    console.log("No tokens to send");
    return '';
  }
  // Notification details.
  const payload = {
    notification: {
      title: 'ACrypto Price Alert',
      body: `${fromCurrency} price has increased to ${currentPrice}`,
      sound: 'default'
    }
  };
  // Set the message as high priority and have it expire after 24 hours.
  var options = {
    priority: "high",
    timeToLive: 60 * 10
  };
  return admin.messaging().sendToDevice(tokens, payload, options)
  .then(function(response) {
    console.log("Successfully sent message:", response);
  })
  .catch(function(error) {
    console.log("Error sending message:", error);
  });
}

// exports.updateInstantId = functions.database.ref('/users/{uid}/instanceId').onWrite(event => {
//   const promises = [];
//   admin.database().ref(`/alerts`).once('value', function(alertSnapshot) {
//     alertSnapshot.forEach(function(fromCurrencySnapshot) {
//       fromCurrencySnapshot.forEach(function(toCurrencySnapshot) {
//         promises.push(createPriceAlertPromise(fromCurrencySnapshot.key, toCurrencySnapshot));
//       });
//     });
//   });
//   return Promise.all(promises);
// });
