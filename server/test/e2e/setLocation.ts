import {browser} from "protractor";

async function setLocation(url) {
    await browser.get(`${`${browser.baseUrl}${url}`}`);
}

export default setLocation;