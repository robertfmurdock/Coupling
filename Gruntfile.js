module.exports = function (grunt) {

  grunt.loadNpmTasks('grunt-env');
  grunt.loadNpmTasks('grunt-mkdir');
  grunt.loadNpmTasks('grunt-karma');
  grunt.loadNpmTasks('grunt-express-server');
  grunt.loadNpmTasks('grunt-protractor-runner');
  grunt.loadNpmTasks('grunt-wiredep');
  grunt.loadNpmTasks('grunt-contrib-watch');

  grunt.initConfig({
    express: {
      options: {},
      dev: {
        options: {
          script: 'server/app.js',
          port: 3001,
          output: 'Express server listening on port'
        }
      },
      dev2: {
        options: {
          script: 'server/app.js',
          port: 3000,
          output: 'Express server listening on port'
        }
      }
    },
    protractor: {
      options: {
        configFile: "test/e2e/protractor-conf.js",
        keepAlive: false,
        noColor: false,
        args: {
          browser: 'chrome'
        }
      },
      chrome: {
        options: {
          args: {
            browser: 'chrome'
          }
        }
      },
      firefox: {
        options: {
          args: {
            browser: 'firefox'
          }
        }
      },
      dockerfirefox: {
        options: {
          args: {
            browser: 'firefox',
            seleniumAddress: 'http://hub:4444/wd/hub'
          }
        }
      },
      dockerchrome: {
        options: {
          args: {
            browser: 'chrome',
            seleniumAddress: 'http://hub:4444/wd/hub'
          }
        }
      },
      safari: {
        options: {
          args: {
            browser: 'safari'
          }
        }
      }
    },
    watch: {
      //gruntfile: {
      //    files: ['Gruntfile.js']
      //},
      servertest: {
        files: [
          "server/**/*",
          "test/endpoint/**/*",
          "test/mocha/**/*"
        ],
        tasks: ['docker-server-test'],
        options: {
          interval: 5007
        }
      },
      frontendtest: {
        files: [
          "public/app/**/*.ts",
          "public/app/**/*.jade",
          "public/images/**/*",
          "public/stylesheets/**/*",
          "server/**/*",
          "views/**/*",
          "!**/*.css",
          "test/jasmine/**/*",
          "test/e2e/**/*"
        ],
        tasks: ['docker-frontend-test'],
        options: {
          interval: 5007
        }
      }
    },
    wiredep: {
      productionTask: {
        src: [
          'views/layout.pug'
        ],
        options: {
          ignorePath: '../public'
        }
      }
    },
    typescript: {
      base: {}
    }
  });

  grunt.registerTask('jenkinsMochaEndpoint', ['env:jenkinsEndpoint', 'mochaTest:jenkinsEndpoint']);

  grunt.registerTask('end2end', ['express:dev', 'protractor:chrome']);

  grunt.registerTask('default', ['unit', 'express:dev', 'mochaTest:endpoint','protractor:chrome', 'protractor:firefox']);
  grunt.registerTask('serve', ['karma:jenkins', 'express:dev', 'jenkinsMochaEndpoint', 'express:dev', 'watch']);

  grunt.registerTask('docker-server-test', ['express:dev', 'mochaTest:endpoint']);
  grunt.registerTask('docker-frontend-test', ['karma:docker', 'express:dev', 'protractor:dockerchrome']);
  grunt.registerTask('dockerserve', ['docker-server-test', 'docker-frontend-test', 'express:dev2', 'watch']);

  grunt.registerTask('wait', function () {
    grunt.log.ok('Waiting for server reload...');
    var done = this.async();
    setTimeout(function () {
      grunt.log.writeln('Done waiting!');
      done();
    }, 1500);
  });
};