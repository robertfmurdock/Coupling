module.exports = function (grunt) {

    grunt.loadNpmTasks('grunt-karma');
    grunt.loadNpmTasks('grunt-protractor-runner');
    grunt.loadNpmTasks('grunt-protractor-webdriver');
    grunt.loadNpmTasks('grunt-express-server');
    grunt.loadNpmTasks('grunt-mocha-test');

    grunt.initConfig({
        karma: {
            unit: {
                configFile: 'karma.conf.js',
                singleRun: true,
                browsers: ['PhantomJS'],
                reporters: ['dots']
            }
        },
        mochaTest: {
            unit: {
                options: {
                    reporter: 'spec'
                },
                src: ['test/mocha/**/*.js']
            },
            endpoint: {
                options: {
                    reporter: 'spec'
                },
                src: ['test/endpoint/**/*.js']
            }
        },
        express: {
            options: {},
            dev: {
                options: {
                    script: 'app.js'
                }
            }
        },
        protractor_webdriver: {
            options: {},
            start: {}
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
            e2e: {},
            jenkins: {
                options: {
                    args: {
                        browser: 'chrome'
                    }
                }
            }
        }
    });

    grunt.registerTask('standard', ['mochaTest:unit', 'karma:unit', 'express:dev', 'mochaTest:endpoint',
        'protractor_webdriver:start']);

    grunt.registerTask('default', ['standard', 'protractor:e2e']);
    grunt.registerTask('jenkins', ['standard']);

};