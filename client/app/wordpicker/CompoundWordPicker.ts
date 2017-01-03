import WordPicker from "./WordPicker";
import * as R from "ramda";

export default class CompoundWordPicker implements WordPicker {

    constructor(public pickers: WordPicker[]) {
    }

    choose(value: string) {
        return R.map(picker => picker.choose(value), this.pickers);
    }

}