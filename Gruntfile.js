module.exports = function (grunt) {

    grunt.loadNpmTasks('grunt-karma');
    grunt.loadNpmTasks('grunt-mocha-test');
    grunt.loadNpmTasks('grunt-express-server');
    grunt.loadNpmTasks('grunt-protractor-runner');
    grunt.loadNpmTasks('grunt-protractor-webdriver');
    grunt.loadNpmTasks('grunt-git-describe');

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
                    script: 'app.js',
                    port: 3001
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
        },
        "git-describe": {
            "options": {
            },
            "jenkins": {
            }
        }
    });

    grunt.registerTask('saveRevision', function () {
            grunt.event.once('git-describe', function (rev) {
                var info = {
                    gitRev: rev.toString(),
                    date: new Date()
                };
                grunt.file.write('version.json', JSON.stringify(info));
                grunt.option('gitRevision', rev);
            });
            grunt.task.run('git-describe');
        }
    );
    grunt.registerTask('markAsDevelopmentBuild', function () {
        grunt.file.write('version.json', JSON.stringify({gitRev: 'DEVELOPMENT' }));
    });

    grunt.registerTask('standard', ['mochaTest:unit', 'karma:unit', 'express:dev', 'mochaTest:endpoint',
        'protractor_webdriver:start']);

    grunt.registerTask('default', ['standard', 'protractor:e2e', 'markAsDevelopmentBuild']);
    grunt.registerTask('jenkins', ['standard', 'saveRevision']);

}
;