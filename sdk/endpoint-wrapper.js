const startJasmine = function () {
  return require('../buildSrc/test-wrapper')
};

process.env.PORT = "3001";

require('../server/build/executable/app').start()
  .then(startJasmine);