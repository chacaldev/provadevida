'use strict'
var axios = require('axios');
var saveVideo = require('./saveVideo');
var validateVideo = require('./util/validateVideo');

var video = function (req, res) {
  var confidence_threshold = 0.75;
  var quality_threshold = -0.2;
  var currentTime = new Date().getTime();
  
  var vid = req.body.video;
  var cpf = req.params.cpf;
  console.log(cpf);
  console.log(vid);
  validateVideo(vid, cpf, function(promise){
    promise
      .then(function(resp) {
        if (resp.data.images && resp.data.images[0]) {
          console.log(JSON.stringify(resp.data.images));
          var transaction = resp.data.images[0].transaction;
          if (transaction.confidence >= confidence_threshold) {
            saveVideo(vid, cpf);
            res.send({
              validation_status: 0,
              subject_id: transaction.subject_id,
              confidence: transaction.confidence,
              quality: transaction.quality
            });
          }
          else if (transaction.quality < quality_threshold) {
            res.send({
              validation_status: 1,
              quality: transaction.quality
            });
          }
          else {
            res.send({
              validation_status: 2,
              confidence: transaction.confidence,
            });
          }
        }
        else {
          console.log(JSON.stringify(resp.data));
          console.log(Array.isArray(resp.data.Errors));
          if(resp.data.Errors && Array.isArray(resp.data.Errors)) {
            var errors = resp.data.Errors;
            var resp = [];
  
            errors.map(function(error) {
              console.log(error.ErrCode);
              if(error.ErrCode === 5002) {
                resp.push({
                  validation_status: -1,
                  mensagem: "Não foram encontradas faces na imagem."
                });
              }
  
              if(error.ErrCode === 5004) {
                resp.push({
                  validation_status: -2,
                  mensagem: "Não foram encontradas imagens para o indivíduo informado."
                });
              }
            });
            console.log(resp);
            res.send({
              erros: resp
            });       
          }
        }      
      });
  });
    
};

module.exports = video;
