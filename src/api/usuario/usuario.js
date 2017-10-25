'use strict'
var express = require('express');
var router = express.Router();
var MongoClient = require('mongodb').MongoClient;

var keys = require('../keys');
var validar = require('./validar_aws');
var imagem = require('./imagem_aws');

router.post('/:cpf/validar', validar);
router.post('/:cpf/imagem', imagem);

router.post('/', function (req, res){
  var currentTime = new Date().getTime();
  var user = {
    nome: req.body.nome,
    nmBeneficio: Number(req.body.nm_beneficio),
    cpf: Number(req.body.cpf),
    dtNascimento: Number(req.body.dt_nascimento),
    createdAt: currentTime,
    updatedAt: currentTime
  }

  MongoClient.connect('mongodb://192.168.0.142:27017/local', function(err, db) {
    if (err) {
      throw err;
    }
    db.collection('user').insert(user, function(err, result) {
      if (err) {
        throw err;
      }
      console.log(result);
      res.send(result);
    });

    db.close();
  });
});

module.exports = router;
