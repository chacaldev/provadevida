var express = require('express');
var app = express();
var bodyParser = require('body-parser');

var hello = require('./api/hello/hello_get');
var usuario = require('./api/usuario/usuario');
var login = require('./api/login/login');

var myLogger = function (req, res, next) {
  console.log('LOGGED');
  next();
};

app.use(myLogger);

app.use(bodyParser.json()); 
app.use(bodyParser.urlencoded({ extended: true }));

app.use('/hello', hello);
app.use('/usuario', usuario);
app.use('/login', login);

app.listen(3000);