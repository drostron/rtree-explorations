SOURCE = rtree-spark-talk.md

all: reveal

clean:
	rm -f index.html

# themes      : default, beige, sky, night, serif, simple, solarized
# transitions : default, linear, cube

reveal: $(SOURCE) $(STYLE)
	pandoc -f markdown -i --smart -t revealjs -V theme=serif -V transition=linear -V controls=false \
		--template=pandoc-templates/default.revealjs --include-in-header=style.html \
		-s $(SOURCE) -o index.html
