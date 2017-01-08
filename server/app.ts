import CouplingDataService from "./lib/CouplingDataService";
import UserDataService from "./lib/UserDataService";
import * as Promise from "bluebird";
import * as express from "express";

const config = require('./../config');

export function start() {
    console.log("Starting express init!");
    const app = express();
    const couplingDataService = new CouplingDataService(config.mongoUrl);
    const userDataService = new UserDataService(couplingDataService.database);

    require('./config/express')(app, userDataService);
    console.log("Adding routing!");
    require('./routes')(app, userDataService, couplingDataService);

    console.log("creating server!");
    return new Promise(function (resolve) {
        const server = app.listen(app.get('port'), function () {
            console.log('Express server listening on port ' + app.get('port'));
            console.log('Deployed at: ' + config.buildDate);
            console.log('Git revision: ' + config.gitRev);
            console.log('Finished Express init!');
            resolve(server);
        });
    });
}