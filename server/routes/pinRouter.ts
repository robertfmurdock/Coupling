"use strict";
import * as express from 'express'
import {handleRequest} from "./route-helper";

class PinRoutes {
    list = handleRequest('performPinsQuery');
    savePin = handleRequest('performSavePinCommand');
    removePin = handleRequest('performDeletePinCommand');
}

const pins = new PinRoutes();
const router = express.Router({mergeParams: true});
router.route('/')
    .get(pins.list)
    .post(pins.savePin);
router.delete('/:pinId', pins.removePin);

export default router