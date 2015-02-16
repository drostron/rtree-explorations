DIST = gh-pages-dist
DESTINATION = $(DIST)/slides/pdxscala-2015-february
SLIDES = slides
REVEALJS = reveal.js

dist:
	mkdir -p $(DESTINATION)/$(REVEALJS)
	cp $(SLIDES)/index.html $(DESTINATION)/
	for i in resources $(REVEALJS)/LICENSE $(REVEALJS)/css $(REVEALJS)/js $(REVEALJS)/lib $(REVEALJS)/plugin; do \
		echo $$i ; \
		cp -R $(SLIDES)/$$i $(DESTINATION)/$$i ; \
	done

push-gh-pages-dist:
	git subtree push --prefix $(DIST) origin gh-pages
