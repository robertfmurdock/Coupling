// @ts-ignore
import * as server from "Coupling-server";

const commandDispatcher = server.com.zegreatrob.coupling.server.commandDispatcher;

export default function () {
    return function (request, response, next) {

        request.statsdKey = ['http', request.method.toLowerCase(), request.path].join('.');
        if (!request.isAuthenticated()) {
            if (request.originalUrl.includes('.websocket')) {
                request.close();
            } else {
                response.sendStatus(401);
            }
        } else {
            commandDispatcher(
                request.user,
                `${request.method} ${request.path}`,
                request.traceId
            ).then(dispatcher => {
                request.commandDispatcher = dispatcher;
                next();
            });
        }
    };
};