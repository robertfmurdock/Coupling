import * as Bluebird from 'bluebird'

function timeSince(start: Date) {
    return new Date().getTime() - start.getTime();
}

export default async function waitFor(isReady: () => boolean, timeout: number) {
    const start = new Date();
    while (!isReady() && (timeSince(start)) < timeout) {
        await new Bluebird(resolve => setTimeout(resolve, 1));
    }

    if ((timeSince(start)) >= timeout) {
        fail(`Was not ready before timeout of ${timeout}.`);
    }
}
