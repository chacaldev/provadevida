'use strict'
var axios = require('axios');

var keys = require('../keys');

var validar = function (req, res) {
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
      console.log(JSON.stringify(resp.data.images));
      res.send(resp.data);
    })
    .catch(function(err) {
      console.log(err);
      res.status(400).send(err);
    });  
};

module.exports = validar;
