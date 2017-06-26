'use strict';

const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp(functions.config().firebase);
const cors = require('cors')({origin: true});
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
  let ref = admin.database().ref(`/master/coins`);

  ref.orderByChild('order')
  .once('value')
  .then(snapshot => {
    var value = snapshot.val();
    if (value) {
        var list = snapshot.val();
        return res.status(200).json({coins: list});
    } else {
        res.status(401).json({error: 'No data found'});
    }
  }).catch(error => {
    console.log('Error getting messages', error.message);
    res.sendStatus(500);
  });
});

// Expose the API as a function
exports.api = functions.https.onRequest(app);