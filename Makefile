.PHONY: run build release

run:
	mvn javafx:run

build:
	mvn clean package

release:
	mvn clean verify