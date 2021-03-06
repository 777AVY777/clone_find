/*
 * (c) Copyright 2015-2017 Micro Focus or one of its affiliates.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * The only warranties for products and services of Micro Focus and its affiliates
 * and licensors ("Micro Focus") are as may be set forth in the express warranty
 * statements accompanying such products and services. Nothing herein should be
 * construed as constituting an additional warranty. Micro Focus shall not be
 * liable for technical or editorial errors or omissions contained herein. The
 * information contained herein is subject to change without notice.
 */

define([
    'find/app/page/search/filters/indexes/indexes-view',
    'databases-view/js/hod-database-helper',
    'js-whatever/js/escape-hod-identifier',
    'find/app/configuration',
    'i18n!find/nls/indexes'
], function(IndexesView, databaseHelper, escapeHodIdentifier, configuration, i18n) {
    'use strict';

    function getPublicIndexIds(enabled) {
        return enabled ?
            [{
                name: 'public',
                displayName: i18n['search.indexes.publicIndexes'],
                className: 'list-unstyled',
                filter: function(model) {
                    return model.get('domain') === 'PUBLIC_INDEXES';
                }
            }]
            : [];
    }

    return IndexesView.extend({
        databaseHelper: databaseHelper,

        getIndexCategories: function() {
            return [{
                name: 'private',
                displayName: i18n['search.indexes.privateIndexes'],
                className: 'list-unstyled',
                filter: function(model) {
                    return model.get('domain') !== 'PUBLIC_INDEXES';
                }
            }].concat(getPublicIndexIds(configuration().publicIndexesEnabled));
        }
    });
});
