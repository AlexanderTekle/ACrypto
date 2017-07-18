'use strict';

const functions = require('firebase-functions');
const admin = require('firebase-admin');
const serviceAccount = require('./service-account.json');
admin.initializeApp({
  credential: admin.credential.cert(serviceAccount),
  databaseURL: `https://${process.env.GCLOUD_PROJECT}.firebaseio.com`
});
const cors = require('cors')({origin: true});
const request = require('request');
const rp = require('request-promise');
const express = require('express');
const Logging = require('@google-cloud/logging');

const app = express();
const logging = Logging();

function logError(message, context = {}) {
  console.error(message, context);
}

function logInfo(message, context = {}) {
  console.log(message, context);
}

function reportError(err, context = {}) {
  const logName = 'errors';
  const log = logging.log(logName);

  // https://cloud.google.com/logging/docs/api/ref_v2beta1/rest/v2beta1/MonitoredResource
  const metadata = {
    resource: {
      type: 'cloud_function',
      labels: { function_name: process.env.FUNCTION_NAME }
    }
  };

  // https://cloud.google.com/error-reporting/reference/rest/v1beta1/ErrorEvent
  const errorEvent = {
    message: err.stack,
    serviceContext: {
      service: process.env.FUNCTION_NAME,
      resourceType: 'cloud_function'
    },
    context: context
  };

  console.error('Error:', context, err);
  // Write the error log entry
  return new Promise((resolve, reject) => {
    log.write(log.entry(metadata, errorEvent), error => {
      if (error) { reject(error); }
      resolve();
    });
  });
}

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

  return admin.database().ref(`/master/coins`).orderByChild("order")
    .once('value').then(snapshot => {
    var value = snapshot.val();
    if (value) {
      var messages = [];
      snapshot.forEach(childSnapshot => {
        if(filter == 1){
          if(!childSnapshot.hasChild(type)){
            return;
          }
        }
        messages.push({code: childSnapshot.key,
          name: childSnapshot.val().name,
          order: childSnapshot.val().order});
      });
      return res.status(200).json({coins: messages});
    } else {
      res.status(401).json({error: 'No data found'});
    }
  }).catch(error => {
    reportError(error, {type: 'http_request', context: req.url});
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

  return query.once('value').then(snapshot => {
    var value = snapshot.val();
    if (value) {
      var messages = [];
      snapshot.forEach(childSnapshot => {
        if(filter == 1){
          if(!childSnapshot.hasChild(type)){
            return;
          }
        }
        messages.push({code: childSnapshot.key,
          name: childSnapshot.val().name,
          order: childSnapshot.val().order});
      });
      return res.status(200).json({currencies: messages});
    } else {
        res.status(401).json({error: 'No data found'});
    }
  }).catch(error => {
    reportError(error, {type: 'http_request', context: req.url});
    res.sendStatus(500);
  });
});

// GET /api/coins_list
// Get all coins list
app.get('/coins_list', (req, res) => {

  return admin.database().ref(`/master/coins_list`)
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
    reportError(error, {type: 'http_request', context: req.url});
    res.sendStatus(500);
  });
});

// GET /api/coins_ignore
// Get all coins ignore
app.get('/coins_ignore', (req, res) => {

  return admin.database().ref(`/master/coins_ignore`)
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
    reportError(error, {type: 'http_request', context: req.url});
    res.sendStatus(500);
  });
});


// GET /api/symbols
// Get symbol of all currency codes
app.get('/symbols', (req, res) => {

  return admin.database().ref(`/master/symbols`)
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
    reportError(error, {type: 'http_request', context: req.url});
    res.sendStatus(500);
  });
});

// GET /api/crons/alert_price
// Triggers price alert check
app.get('/crons/alerts/price', (req, res) => {
  return admin.database().ref(`/crons/alerts/price`).set(admin.database.ServerValue.TIMESTAMP).then(result => {
    return res.status(200).json({alerts: 'triggered'});
  })
  .catch(error => {
    reportError(error, {type: 'http_request', context: 'price alert cron'});
    res.sendStatus(500);
  });
});

// GET /api/amazontoken
// Triggers price alert check for testing
app.get('/amazontoken', (req, res) => {
  const userId = req.query.userid;
  const accessToken = req.query.accesstoken;
  if(!userId || !accessToken){
    return res.status(400).json({error: 'Something is missing'});
  }
  return rp(generateAmazonApiRequest(accessToken),
  {resolveWithFullResponse: true}).then(response => {
    if (response.statusCode === 200) {
      const result = JSON.parse(response.body);
      const name = result.name;
      const uid = result.user_id;
      const email = result.email;
      if(uid != userId){
        return res.status(403).json({error: 'Unauthorized'});
      }
      // Create a Firebase account and get the Custom Auth Token.
      createFirebaseAccount(uid, name, email).then(firebaseToken => {
        if(!firebaseToken){
          return res.status(409).json({error: 'Already exists', email});
        }
        return res.status(200).json({firebase_token: firebaseToken});
      }).catch(error => {
        reportError(error, { type: 'auth', context: 'fiebase account'});
        return res.status(500).json({error: 'Server error', error});
      });
    } else {
      return res.status(response.statusCode).json({error: response.body});
    }
  }).catch(error => {
    reportError(error, { type: 'http_request', context: 'amazon profile'});
    return res.status(403).json({error: 'Authentication error: Cannot verify access token', error});
  });
});

// GET /api/test/crons/alert_price
// Triggers price alert check for testing
app.get('/tests/crons/alerts/price', (req, res) => {
  return admin.database().ref(`/alerts/price`).once('value').then(alertSnapshot => {
    const promises = [];
    const iamUser = functions.config().iamuser.userid;
    alertSnapshot.forEach(function(dataSnapshot) {
      dataSnapshot.forEach(function(data) {
        const userId = data.key;
        if(userId == iamUser){
          promises.push(createPriceAlertPromise(dataSnapshot));
        }
      });
    });
    return Promise.all(promises).then(results => {
        return res.status(200).json({alerts: 'triggered'});
    });
  });
});

app.get('/tests/query', (req, res) => {
  return admin.database().ref(`/user_alerts/price`).once('value').then(alertSnapshot => {
    var messages = [];
    var count = 0;
    alertSnapshot.forEach(function(dataSnapshot) {
      dataSnapshot.forEach(function(data) {
        const userId = dataSnapshot.key;
        const key = data.key;
        const name = data.val().name;
        const status = data.val().status;
        const nameStatusIndex = data.val().nameStatusIndex;
        if(!nameStatusIndex){
          count++;
          messages.push({user: userId, comboKey:name });
          admin.database().ref(`/user_alerts/price/${userId}/${key}/nameStatusIndex`).set(name + status);
        }
      });
    });
    return res.status(200).json({count: count, query: messages});
  });
});

// Expose the API as a function
exports.api = functions.https.onRequest(app);

// Checks price alerts for users
exports.priceAlertCheck = functions.database.ref('/crons/alerts/price').onWrite(event => {
  return admin.database().ref(`/alerts/price`).once('value').then(alertSnapshot => {
    const promises = [];
    alertSnapshot.forEach(function(dataSnapshot) {
      promises.push(createPriceAlertPromise(dataSnapshot));
    });
    return Promise.all(promises);
  });
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
  return rp(createPriceUrl(fromCurrency, toCurrency, exchange),
  {resolveWithFullResponse: true}).then(response => {
    if (response.statusCode === 200) {
      const jsonobj = JSON.parse(response.body);
      const currentPrice = jsonobj[toCurrency];
      const promises = [];

      snapshot.forEach(function(data) {
        promises.push(sendAlertNotifications(snapshot.key, data.key, currentPrice));
      });
      return Promise.all(promises);
    }
    throw response.body;
  }).catch(error => {
    return reportError(error, { type: 'http_request', context: 'price fetching'});
  });
}

function sendAlertNotifications(comboKey, userId, currentPrice) {
  const getUserPromise = admin.database()
                          .ref(`/users/${userId}`)
                          .once('value');
  const getUserPriceAlertsPromise = admin.database()
                          .ref(`/user_alerts/price/${userId}`)
                          .orderByChild('nameStatusIndex')
                          .equalTo(comboKey+'1')
                          .once('value');
  return Promise.all([getUserPromise, getUserPriceAlertsPromise]).then(results => {
    const userSnapshot = results[0];
    if(!userSnapshot.val()){
      return logError('User not found', {user: userId})
    }
    const instanceId = userSnapshot.val().instanceId;
    const subscriptionStatus = userSnapshot.val().subscriptionStatus;
    const priceAlertSnapshot = results[1];
    if(subscriptionStatus != 1){
      //return logInfo("Subscription expired", {user: userId});
    }
    //we removed an invalid instanceId, so just return
    if(!instanceId){
      return logInfo("No instanceId", {user: userId});
    }
    // Check if there are any device tokens.
    if (!priceAlertSnapshot.hasChildren()) {
      // TODO: remove the corresponding /alerts/price
      return logInfo("No alerts to send", {user: userId, key : comboKey});
    }
    logInfo("Alerts fetched", {user: userId, alert_count: priceAlertSnapshot.numChildren(), key : comboKey});
    const promises = [];
    priceAlertSnapshot.forEach(function(dataSnapshot) {
        promises.push(sendAlertNotification(userId, instanceId, currentPrice, dataSnapshot));
    });
    return Promise.all(promises);
  })
  .catch(error => {
    return reportError(error, {user: userId, type: 'database_query', context: 'user alerts'});
  });
}

function sendAlertNotification(userId, instanceId, currentPrice, dataSnapshot) {
  const comboKey = dataSnapshot.val().name;
  const comboKeyArray = comboKey.split('-');
  const fromCurrency = comboKeyArray[0];
  const toCurrency = comboKeyArray[1];
  const exchange = comboKeyArray[2];
  const alertPrice = dataSnapshot.val().value;
  const condition = dataSnapshot.val().condition;
  const toSymbol = dataSnapshot.val().toSymbol;
  const frequency = dataSnapshot.val().frequency;

  if(!priceAlertConditionCheck(currentPrice, dataSnapshot)) {
    return;
  }

  // Notification details.
  const payload = {
    notification: {
      title: `${fromCurrency}/${toCurrency} Price Alert`,
      body: getPriceAlertBody(currentPrice, alertPrice, toSymbol, condition, exchange),
      sound: 'default',
      tag: comboKey
    },
    data: {
      title: `${fromCurrency}/${toCurrency} Price Alert`,
      body: getPriceAlertBody(currentPrice, alertPrice, toSymbol, condition, exchange),
      name: comboKey,
      sound: 'default',
      type: "alert"
    }
  };
  // Set the message as high priority and have it expire after 24 hours.
  var options = {
    priority: "high",
    timeToLive: 60 * 10
  };

  if (frequency == 'Onetime') {
    dataSnapshot.ref.update({ status: 0, nameStatusIndex: comboKey + "0" });
  }

  return sendNotification(userId, instanceId, payload, options);
}

function sendNotification(userId, instanceId, payload, options) {
  return admin.messaging().sendToDevice(instanceId, payload, options).then(response => {
    response.results.forEach((result, index) => {
      const error = result.error;
      if (error) {
        reportError(error, {user: userId, token: instanceId});
        if (error.code === 'messaging/invalid-registration-token' ||
            error.code === 'messaging/registration-token-not-registered') {
          return admin.database().ref(`/users/${userId}/instanceId`).remove().then(result => {
            logInfo('Removed invalid instanceId', {user: userId, token: instanceId});
          })
          .catch(error => {
            return reportError(error, {user: uid, type: 'database_write', context: 'delete instanceId'});
          });
        }
      }
      return logInfo("Successfully sent message", {user: userId, respnse : response});
    });
  })
  .catch(error => {
    return reportError(error, {user: userId, token: instanceId, type: 'fcm_message'});
  });
}

function getPriceAlertBody(currentPrice, alertPrice, toSymbol, condition, exchange) {
  return toSymbol + currentPrice + " (" + getPriceDiff(currentPrice, alertPrice) +
    "% " + getConditionSymbol(condition) + ")" +
    (exchange ? " on " + exchange : "");
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
  var diff = Math.abs((currentPrice - alertPrice) / alertPrice);
  return round(diff * 100, 2);
}

function round(value, decimals) {
  return Number(Math.round(value + 'e' + decimals) + 'e-' + decimals);
}

exports.createPriceAlert = functions.database.ref('/user_alerts/price/{uid}/{alertId}').onCreate(event => {
  const snapshot = event.data;
  const uid = event.params.uid;
  const comboKey = snapshot.current.val().name;

  return admin.database().ref(`/alerts/price/${comboKey}/${uid}`).set(true).then(result => {
    logInfo("Create alert", {user: uid, key : comboKey});
  })
  .catch(error => {
    return reportError(error, {user: uid, type: 'database_write', context: 'add alert'});
  });
});

exports.deletePriceAlert = functions.database.ref('/user_alerts/price/{uid}/{alertId}').onDelete(event => {
  const snapshot = event.data;
  const uid = event.params.uid;

  const comboKey = snapshot.previous.val().name;
  return admin.database().ref(`/alerts/price/${comboKey}/${uid}`).remove().then(result => {
    logInfo("Delete alert", {user: uid, key : comboKey});
  })
  .catch(error => {
    return reportError(error, {user: uid, type: 'database_write', context: 'delete alert'});
  });
});

exports.updateOnPriceAlertName = functions.database.ref('/user_alerts/price/{uid}/{alertId}/name').onUpdate(event => {
  const snapshot = event.data;
  const uid = event.params.uid;
  const name = snapshot.current.val();
  const prevName = snapshot.previous.val();

  const removePromise = admin.database().ref(`/alerts/price/${prevName}/${uid}`).remove();
  const addPromise = admin.database().ref(`/alerts/price/${name}/${uid}`).set(true);
  return Promise.all([removePromise, addPromise]).then(result => {
    logInfo("Removed and Added alert", {user: uid, key : name});
  })
  .catch(error => {
    return reportError(error, {user: uid, type: 'database_write', context: 'update alert'});
  });
});

exports.updateOnPriceAlertStatus = functions.database.ref('/user_alerts/price/{uid}/{alertId}/status').onUpdate(event => {
  const snapshot = event.data;
  const uid = event.params.uid;
  const alertId = event.params.alertId;
  const status = snapshot.current.val();

  return admin.database().ref(`/user_alerts/price/${uid}/${alertId}`).once('value').then(snapshot => {
    const name = snapshot.val().name;
    if(status == 1){
        logInfo("Added alert on status change", {user: uid, key : name});
        return admin.database().ref(`/alerts/price/${name}/${uid}`).set(true);
    } else {
        logInfo("Removed alert on status change", {user: uid, key : name});
        return admin.database().ref(`/alerts/price/${name}/${uid}`).remove();
    }
  });
});

// Generate a Request option to access Amazon APIs
function generateAmazonApiRequest(accessToken) {
  return "https://api.amazon.com/user/profile?access_token=" + accessToken;
}

function createFirebaseAccount(uid, displayName, email){
  const userId = uid.split('.').join('-');
  // Create or update the user account.
  const userCreationTask = admin.auth().updateUser(userId, {
    displayName: displayName,
    email: email
  }).then(result => {
    return result;
  })
  .catch(error => {
    if (error.code === 'auth/user-not-found') {
      return admin.auth().createUser({
        uid: userId,
        displayName: displayName,
        email: email
      });
    } else if (error.code === 'auth/email-already-exists') {
      return "";
    }
    throw error;
  });

  return userCreationTask.then(result => {
    // Create a Firebase custom auth token.
    return admin.auth().createCustomToken(userId).then((token) => {
      console.log('Created Custom token for UID "', userId, '" Token:', token);
      return token;
    });
  });
}
