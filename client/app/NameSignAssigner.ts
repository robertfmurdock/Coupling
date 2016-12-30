import * as R from "ramda";

export default class CallSignAssigner {

    constructor(public callSigns: string[]) {
    }

    choose(value: string) {
        const index = this.getNameIndexForString(value);
        return this.callSigns[index];
    }

    private getNameIndexForString(value: string) {
        const number = R.sum(this.getIntegersFromChars(value));

        return number % this.callSigns.length;
    }

    private getIntegersFromChars(value: string) {
        function characterToInteger(character: string) {
            return character.charCodeAt(0);
        }

        return R.map(characterToInteger, value.split(''));
    }
}