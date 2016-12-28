"use strict";
import Player from "./Player";

export default class PairAssignmentDocument {
    constructor(public date: Date| string, public pairs: Player[][], public tribe: string) {
    }
}
