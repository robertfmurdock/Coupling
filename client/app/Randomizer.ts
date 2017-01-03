export default class Randomizer {

    next(maxValue: number) {
        const floatValue = Math.random() * maxValue;
        return Math.round(floatValue);
    }
}
