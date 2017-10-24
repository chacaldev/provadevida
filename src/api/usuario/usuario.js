'use strict'
var express = require('express');
var router = express.Router();
var axios = require('axios');

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
      'app_id': 'efe6cc00',
      'app_key': 'f86cadc55991210addba87d1d8736658',
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

module.exports = router;
