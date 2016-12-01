var webpack = require('webpack');
var Jasmine = require('jasmine');
var reporters = require('jasmine-reporters');
var config = require('./webpack.config');
var fs = require('fs-extra');

webpack(config)
  .run(function (err, stats) {
    console.log(stats.toString('minimal'));
    if (err) {
      throw err;
    }
    console.log('Starting tests:');

    var jasmine = new Jasmine();

    jasmine.loadConfig({
      "spec_dir": "test/unit/server",
      "spec_files": [
        "./.tmp/test.js"
      ],
      "stopSpecOnExpectationFailure": false,
      "random": false
    });

    jasmine.configureDefaultReporter({});

    var junitReporter = new reporters.JUnitXmlReporter({
      savePath: __dirname + '/../../../test-output',
      filePrefix: 'server.unit',
      consolidateAll: true
    });

    jasmine.addReporter(junitReporter);

    jasmine.addReporter({
      jasmineDone: function(){
        fs.removeSync(__dirname + '/.tmp');
      }
    });
    jasmine.execute();
  });