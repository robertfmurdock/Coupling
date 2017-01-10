import CompoundWordPicker from "./CompoundWordPicker";
import RandomWordPicker from "./RandomWordPicker";

import CallSignData from './CallSignData';

const {adjectives, nouns} = CallSignData;

export default class CallSignPicker {

    private picker: CompoundWordPicker;

    constructor() {
        const adjectivePicker = new RandomWordPicker(adjectives);
        const nounPicker = new RandomWordPicker(nouns);
        this.picker = new CompoundWordPicker([adjectivePicker, nounPicker]);
    }


    pick(email: string) {
        const [adjective, noun] = this.picker.choose(email);
        return {
            adjective: adjective,
            noun: noun
        }
    }
}