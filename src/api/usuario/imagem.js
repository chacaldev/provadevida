'use strict'
var axios = require('axios');

var keys = require('../keys');
var saveImage = require('./save');

var imagem = function (req, res) {
  var config = {
    headers: {
      'Content-Type': 'application/json',
      'app_id': keys.app_id,
      'app_key': keys.app_key,
    }
  };

  var currentTime = new Date().getTime();
  
  var img = req.body.imagem;
  var cpf = req.params.cpf;

  var payload = {
    "image": img,
    "gallery_name":"gal-" + cpf,
    "subject_id": currentTime
  };

  axios.post('https://api.kairos.com/enroll', payload, config)
    .then(function(resp) {
      console.log(resp.data);
      saveImage(img, cpf);
      res.send(resp.data);
    })
    .catch(function(err) {
      console.log(err);
      res.send(err);
  });
};

module.exports = imagem;
