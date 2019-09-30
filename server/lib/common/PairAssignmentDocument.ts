"use strict";
import Pair from "./Pair";

export default class PairAssignmentDocument {
    constructor(public date: Date| string, public pairs: Pair[]) {
    }
}
