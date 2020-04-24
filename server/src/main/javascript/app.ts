import UserDataService from "./lib/UserDataService";
import * as express from "express";
import * as expressWs from "express-ws";

const config = require('./config/config');
const serverKt = require("Coupling-server");

const {configureExpressKt, configRoutes, logStartup} = serverKt.com.zegreatrob.coupling.server;

function listen(app) {
    return new Promise(function (resolve) {
        const server = app.listen(app.get('port'), function () {
            // noinspection JSUnresolvedVariable, JSUnresolvedFunction
            logStartup(
                app.get('port'),
                config.buildDate,
                config.gitRev,
                app.get('env')
            );

            resolve(server);
        });
    });
}

export async function start() {
    const wsInstance = expressWs(express());
    const app = wsInstance.app;
    const userDataService = new UserDataService();

    configureExpressKt(app)
    require('./config/express')(app, userDataService);
    configRoutes(wsInstance)

    return await listen(app);
}