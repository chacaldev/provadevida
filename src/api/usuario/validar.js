'use strict'
var axios = require('axios');

var keys = require('../keys');
var saveImage = require('./save');

var validar = function (req, res) {
  var confidence_threshold = 0.9;
  var quality_threshold = 0.3;
  var config = {
    headers: {
      'Content-Type': 'application/json',
      'app_id': keys.app_id,
      'app_key': keys.app_key,
    }
  };

  var img = req.body.imagem;
  var cpf = req.params.cpf;

  var payload = {
    "image": img,
    "gallery_name":"gal-" + cpf,
  };
  // CÓDIGOS DE VALIDAÇÃO
  // 0: validação OK
  // Valores positivos tiveram retorno da API, porém não foram validados pelos 
  // limites estabelecidos na aplicação.
  //
  // 1: baixa qualidade
  // 2: baixa confiança
  //
  // Valores negativos indicam erros da API
  //
  // -1: não encontrado faces na imagem
  // -2: não encontrado galeria com o nome informado
  //
  axios.post('https://api.kairos.com/recognize', payload, config)
    .then(function(resp) {
      if (resp.data.images && resp.data.images[0]) {
        console.log(JSON.stringify(resp.data.images));
        var transaction = resp.data.images[0].transaction;
        if (transaction.confidence >= confidence_threshold) {
          saveImage(img, cpf);
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
        if(resp.data.Errors && Array.isArray(resp.data.Errors)) {
          var errors = resp.data.Errors;
          var resp = [];

          errors.map(function(error) {
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

            res.status(400).send({
              erros: resp
            });
          });       
        }
      }      
    })
    .catch(function(err) {
      console.log(err);
      res.status(400).send(err);
    });  
};

module.exports = validar;
