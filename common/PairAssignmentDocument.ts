"use strict";
import Player from "./Player";

export default class PairAssignmentDocument {
    constructor(public date: Date, public pairs : [[Player]]) {
    }
}
