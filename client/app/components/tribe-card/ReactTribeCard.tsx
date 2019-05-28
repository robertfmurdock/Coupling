import * as React from "react";
import * as classNames from 'classnames'
import Tribe from "../../../../common/Tribe";
import * as styles from './styles.css'
import {fitHeaderText} from "../ReactFittyHelper";
import GravatarImage from "../gravatar/GravatarImage";
import PathSetter from "../PathSetter";



interface Props {
    tribe: Tribe,
    size: number,
    pathSetter: PathSetter
}

export default class ReactTribeCard extends React.Component<Props> {

    static defaultProps = {
        size: 150
    };

    render() {
        return <span
            className={classNames("tribe-card", styles.className)}
            onClick={() => this.onClick()}
            tabIndex={0}
            style={this.cardStyle()}
        >
            {this.cardHeader()}
            {this.gravatarImage()}
        </span>;
    }

    private onClick() {
        this.props.pathSetter(`/${this.props.tribe.id}/pairAssignments/current`)
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

    private cardHeader() {
        const {tribe} = this.props;
        return <div style={this.headerStyle()} className={"tribe-card-header"}>
            <div className={styles.header} onClick={event => this.onClickHeader(event)}>
                <div>{tribe.name || "Unknown"}</div>
            </div>
        </div>;
    }

    private headerStyle() {
        const {size} = this.props;
        const headerMargin = size * 0.02;
        const maxHeaderHeight = size * 0.35;
        return {
            margin: `${headerMargin}px 0 0 0`,
            'height': `${maxHeaderHeight}px`
        };
    }

    private onClickHeader(event: React.MouseEvent<HTMLDivElement>) {
        if (event.stopPropagation) event.stopPropagation();
        this.props.pathSetter(`/${this.props.tribe.id}/edit`)
    }

    private gravatarImage() {
        const {tribe, size} = this.props;
        return <GravatarImage
            email={tribe.email}
            fallback={"/images/icons/tribes/no-tribe.png"}
            alt={"tribe-img"}
            options={{size, default: 'identicon'}}
        />;
    }

    componentDidMount(): void {
        this.fitHeader();
    }

    componentDidUpdate(prevProps: Readonly<Props>, prevState: Readonly<{}>, snapshot?: any): void {
        this.fitHeader();
    }

    private fitHeader() {
        const {size} = this.props;
        const maxFontHeight = (size * 0.15);
        const minFontHeight = (size * 0.16);
        fitHeaderText(maxFontHeight, minFontHeight, this, styles.header);
    }
}