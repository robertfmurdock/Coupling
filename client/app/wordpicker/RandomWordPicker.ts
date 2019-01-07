import WordPicker from './WordPicker'
import * as map from "ramda/src/map";
import * as sum from "ramda/src/sum";

export default class RandomWordPicker implements WordPicker {

    constructor(public callSigns: string[]) {
    }

    choose(value: string) {
        const index = this.getNameIndexForString(value);
        return this.callSigns[index];
    }

    private getNameIndexForString(value: string) {
        const number = sum(this.getIntegersFromChars(value));

        return number % this.callSigns.length;
    }

    private getIntegersFromChars(value: string) {
        function characterToInteger(character: string) {
            return character.charCodeAt(0);
        }

        return map(characterToInteger, value.split(''));
    }
}