import historyRouter from './historyRouter'
import pinsRouter from './pinRouter'
import * as express from 'express'
let players = require('./players');
let spin = require('./spin');

let router = express.Router({mergeParams: true});
router.post('/spin', spin);
router.use('/history', historyRouter);
router.use('/players', players);
router.use('/pins', pinsRouter);
export default router