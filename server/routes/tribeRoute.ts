import * as express from "express";
import historyRouter from "./historyRouter";
import pinsRouter from "./pinRouter";
import playerRouter from "./playerRouter";
import spin from './spin';

let router = express.Router({mergeParams: true});
router.post('/spin', spin);
router.use('/history', historyRouter);
router.use('/players', playerRouter);
router.use('/pins', pinsRouter);
export default router