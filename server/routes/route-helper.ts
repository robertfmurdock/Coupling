export function handleRequest(commandRunFunction) {
    return (request, response) => {
        request.commandDispatcher[commandRunFunction](request, response);
    }
}
