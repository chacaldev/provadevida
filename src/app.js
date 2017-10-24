var express = require('express');
var app = express();
var hello = require('./api/hello/hello_get');

var myLogger = function (req, res, next) {
  console.log('LOGGED');
  next();
};

app.use(myLogger);

app.use('/hello', hello);

app.listen(3000);