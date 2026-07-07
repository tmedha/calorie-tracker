package com.calorietracker.ui.controls;

import com.calorietracker.util.NumberFmt;
import javafx.geometry.Pos;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.Text;

/**
 * A circular calorie meter: a translucent track ring with a white progress arc sweeping
 * clockwise from the top, and the eaten total plus target caption in the center.
 * Designed to sit on the blue hero card.
 */
public class CalorieRing extends StackPane {

    private static final double SIZE = 156;
    private static final double STROKE = 14;

    private final Arc progress;
    private final Text value = new Text("0");
    private final Text caption = new Text("kcal");

    public CalorieRing() {
        double radius = (SIZE - STROKE) / 2;

        Arc track = arc(radius);
        track.setStroke(Color.web("white", 0.22));
        track.setLength(360);

        progress = arc(radius);
        progress.setStroke(Color.WHITE);
        progress.setLength(0);

        Pane ring = new Pane(track, progress);
        ring.setMinSize(SIZE, SIZE);
        ring.setPrefSize(SIZE, SIZE);
        ring.setMaxSize(SIZE, SIZE);

        value.getStyleClass().add("ring-value");
        caption.getStyleClass().add("ring-caption");
        VBox center = new VBox(2, value, caption);
        center.setAlignment(Pos.CENTER);

        getChildren().addAll(ring, center);
        setMinSize(SIZE, SIZE);
        setPrefSize(SIZE, SIZE);
        setMaxSize(SIZE, SIZE);
    }

    /** Updates the ring: fills toward {@code target}, clamped at a full circle. */
    public void setValues(double eaten, int target) {
        double fraction = target > 0 ? Math.min(1.0, eaten / target) : 0;
        progress.setLength(-fraction * 360);
        value.setText(NumberFmt.whole(eaten));
        caption.setText(target > 0 ? "of " + target + " kcal" : "kcal");
    }

    private Arc arc(double radius) {
        Arc a = new Arc(SIZE / 2, SIZE / 2, radius, radius, 90, 0);
        a.setType(ArcType.OPEN);
        a.setFill(Color.TRANSPARENT);
        a.setStrokeWidth(STROKE);
        a.setStrokeLineCap(StrokeLineCap.ROUND);
        return a;
    }
}
