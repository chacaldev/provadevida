'use strict'
var express = require('express');
var router = express.Router();
var MongoClient = require('mongodb').MongoClient;

router.post('/', function (req, res){
  var cpf = Number(req.body.cpf);
  var nmBeneficio = Number(req.body.nm_beneficio);
  var dtNascimento =  Number(req.body.dt_nascimento);

  MongoClient.connect('mongodb://192.168.0.79:27017/local', function(err, db) {
    if (err) {
      throw err;
    }
    db.collection('user').findOne({ cpf: cpf}, function(err, result) {
      if (err) {
        throw err;
      }

      if (!result || result.nmBeneficio !== nmBeneficio || result.dtNascimento !== dtNascimento){
        res.send('Dados inválidos');
      }
      else {
        console.log(result);
        res.send(result)
      }
    });

    db.close();
  });
});

module.exports = router;
