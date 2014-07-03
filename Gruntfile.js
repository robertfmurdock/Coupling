module.exports = function (grunt) {

    grunt.loadNpmTasks('grunt-karma');
    grunt.loadNpmTasks('grunt-protractor-runner');
    grunt.loadNpmTasks('grunt-protractor-webdriver');
    grunt.loadNpmTasks('grunt-express-server');

    grunt.initConfig({
        karma: {
            unit: {
                configFile: 'karma.conf.js',
                singleRun: true,
                browsers: ['PhantomJS'],
                reporters: ['dots']
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
                args: {}
            },
            e2e: {}
        }
    });

    grunt.registerTask('default', ['karma:unit', 'express:dev', 'protractor_webdriver:start', 'protractor:e2e']);

};