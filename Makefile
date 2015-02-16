DIST = gh-pages-dist
SLIDES = slides
REVEALJS = reveal.js
DIST_SLIDES = $(DIST)/$(SLIDES)

dist:
	mkdir -p $(DIST_SLIDES)/$(REVEALJS)
	cp $(SLIDES)/index.html $(DIST_SLIDES)/
	for i in resources $(REVEALJS)/LICENSE $(REVEALJS)/css $(REVEALJS)/js $(REVEALJS)/lib $(REVEALJS)/plugin; do \
		echo $$i ; \
		cp -R $(SLIDES)/$$i $(DIST_SLIDES)/$$i ; \
	done

push-gh-pages-dist:
	git subtree push --prefix $(DIST) origin gh-pages
