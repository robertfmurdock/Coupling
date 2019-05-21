import * as React from "react";
import * as classNames from 'classNames'
import Tribe from "../../../../common/Tribe";
import * as styles from './styles.css'
import {tribeGravatarUrl} from "../player-card/GravatarHelper";
import {fitHeaderText} from "../ReactFittyHelper";

interface Props {
    tribe: Tribe,
    size: number,
    pathSetter: (string) => void
}

export default class ReactTribeCard extends React.Component<Props> {

    static defaultProps = {
        size: 150
    };

    render() {
        const {tribe, size} = this.props;

        return <span
            className={classNames("tribe-card", styles.className)}
            onClick={() => this.onClick()}
            tabIndex={0}
            style={this.cardStyle()}
        >
            <div style={this.headerStyle()} className={"tribe-card-header"}>
                <div
                    className={styles.header}
                    onClick={event => this.onClickHeader(event)}
                >
                    <div>
                        {tribe.name || "Unknown"}
                    </div>
                </div>
            </div>
            <img src={tribeGravatarUrl(tribe, {size})} width={size} height={size} alt={"tribe-img"}/>
        </span>;
    }

    fitHeader() {
        const {size} = this.props;
        const maxFontHeight = (size * 0.15);
        const minFontHeight = (size * 0.16);
        fitHeaderText(maxFontHeight, minFontHeight, this, styles.header);
    }

    componentDidMount(): void {
        this.fitHeader();
    }

    componentDidUpdate(prevProps: Readonly<Props>, prevState: Readonly<{}>, snapshot?: any): void {
        this.fitHeader();
    }

    private onClick() {
        this.props.pathSetter(`/${this.props.tribe.id}/pairAssignments/current`)
    }

    private onClickHeader(event: React.MouseEvent<HTMLDivElement>) {
        if (event.stopPropagation) event.stopPropagation();
        this.props.pathSetter(`/${this.props.tribe.id}/edit`)
    }

    private cardStyle() {
        const {size} = this.props;
        const pixelWidth = size;
        const pixelHeight = (size * 1.4);
        const paddingAmount = (size * 0.02);
        const borderAmount = (size * 0.01);
        return {
            width: `${pixelWidth}px`,
            height: `${pixelHeight}px`,
            padding: `${paddingAmount}px`,
            'border-width': `${borderAmount}px`,
        };
    }

    private headerStyle() {
        const {size} = this.props;
        const headerMargin = (size * 0.02);
        const maxHeaderHeight = size * 0.35;
        return {
            margin: `${headerMargin}px 0 0 0`,
            'height': `${maxHeaderHeight}px`
        };
    }
}