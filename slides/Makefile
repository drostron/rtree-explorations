SOURCE = ../target/scala-2.11/tut/slides.md

all: reveal

clean:
	rm -f index.html

# themes      : default, beige, sky, night, serif, simple, solarized
# transitions : default, none, fade, slide, convex, concave, zoom

reveal: $(SOURCE) $(STYLE)
	pandoc -f markdown+escaped_line_breaks -i --smart -t revealjs -V theme=serif -V transition=fade -V controls=false \
		--template=pandoc-templates/default.revealjs \
		--include-in-header=style.html --highlight-style=zenburn \
		--include-after-body=after-body.html \
		-s $(SOURCE) -o index.html
