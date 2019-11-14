export function handleRequest(commandRunFunction) {
    return (request, response) => {
        let commandDispatcher = request.commandDispatcher;
        commandRunFunction(commandDispatcher, request, response);
    }
}
