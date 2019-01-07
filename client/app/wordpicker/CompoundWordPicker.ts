import WordPicker from "./WordPicker";
import * as map from "ramda/src/map";

export default class CompoundWordPicker implements WordPicker {

    constructor(public pickers: WordPicker[]) {
    }

    choose(value: string) {
        return map(picker => picker.choose(value), this.pickers);
    }

}