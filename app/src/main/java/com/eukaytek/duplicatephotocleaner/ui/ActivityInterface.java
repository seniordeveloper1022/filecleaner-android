package com.eukaytek.duplicatephotocleaner.ui;

import com.eukaytek.duplicatephotocleaner.ui.search.SearchViewModel;
import com.eukaytek.duplicatephotocleaner.ui.searchresults.ResultsViewModel;

public interface ActivityInterface {
    ResultsViewModel getResultViewModel();

    SearchViewModel getSearchViewModel();
}
