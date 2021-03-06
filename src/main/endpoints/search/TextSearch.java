package main.endpoints.search;

import main.model.TextSearchResult;
import main.phraseSearching.PhraseSearch;
import main.utilities.DescriptionGetter;

import java.util.ArrayList;
import java.util.List;

public class TextSearch extends Search<TextSearchResult> {

    public TextSearch() {
        super(true);
    }

    @Override
    public List<TextSearchResult> rank(List<String> words, String country, String user) {
        return ranker.rankText(words, country, user);
    }

    @Override
    public List<TextSearchResult> phraseSearch(String phrase, String country, String user, List<String> stemmedQueryWords) {
        return PhraseSearch.search(ranker, phrase, country, stemmedQueryWords);
    }

    @Override
    public void describe(List<TextSearchResult> results, List<String> stemmedQueryWords) {
        ArrayList<String> urls = new ArrayList<>(results.size());
        for (TextSearchResult result : results)
            urls.add(result.getUrl());
        List<String> descriptions = DescriptionGetter.getDescriptions(urls, stemmedQueryWords);
        for (int i = 0; i < urls.size(); i++)
            results.get(i).setDescription(descriptions.get(i));
    }
}