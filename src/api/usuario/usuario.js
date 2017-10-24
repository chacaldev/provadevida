'use strict'
var express = require('express');
var router = express.Router();
var axios = require('axios');
var MongoClient = require('mongodb').MongoClient;

var keys = require('../keys');

router.post('/:cpf/validar', function (req, res) {
  var config = {
    headers: {
      'Content-Type': 'application/json',
      'app_id': keys.app_id,
      'app_key': keys.app_key,
    }
  };

  var payload = {
    "image": req.body.imagem,
    "gallery_name":"gal-" + req.params.cpf,
    "subject_id": req.params.cpf
  };

  axios.post('https://api.kairos.com/verify', payload, config)
    .then(function(resp) {
      console.log(resp.data);
      res.send(resp.data);
    })
    .catch(function(err) {
      console.log(err);
      res.send(err);
    });  
});

router.post('/:cpf/imagem', function (req, res) {
  var config = {
    headers: {
      'Content-Type': 'application/json',
      'app_id': keys.app_id,
      'app_key': keys.app_key,
    }
  };
  
  var payload = {
    "image": req.body.imagem,
    "gallery_name":"gal-" + req.params.cpf,
    "subject_id": req.params.cpf
  };

  axios.post('https://api.kairos.com/enroll', payload, config)
    .then(function(resp) {
      console.log(resp.data);
      res.send(resp.data);
    })
    .catch(function(err) {
      console.log(err);
      res.send(err);
    });  
});

router.post('/', function (req, res){
  var currentTime = new Date().getTime();
  var user = {
    nome: req.body.nome,
    nmBeneficio: req.body.nm_beneficio,
    cpf: req.body.cpf,
    dtNascimento: req.body.dt_nascimento,
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
      res.send(result)
    });
  });
});

module.exports = router;
