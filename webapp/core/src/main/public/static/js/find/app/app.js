/*
 * Copyright 2014-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'jquery',
    'backbone',
    'underscore',
    'dropzone',
    'find/app/util/test-browser',
    'find/app/model/window-scroll-model',
    'find/app/model/saved-searches/saved-query-collection',
    './model-registry',
    'find/app/navigation',
    'find/app/configuration',
    'find/app/pages',
    'find/app/util/logout',
    'find/app/vent',
    'find/app/router',
    'text!find/templates/app/app.html'
], function($, Backbone, _, Dropzone, testBrowser, WindowScrollModel, SavedQueryCollection, ModelRegistry,
            Navigation, configuration, Pages, logout, vent, router, template) {

    return Backbone.View.extend({
        el: '.page',
        template: _.template(template),

        // Can be overridden
        defaultPage: null,
        Navigation: Navigation,
        IndexesCollection: null,

        // Abstract
        getPageData: null,

        events: {
            'click .navigation-logout': function() {
                logout('../logout');
            }
        },

        initialize: function() {
            $.ajaxSetup({cache: false});

            // disable auto-discover for dropzones
            Dropzone.autoDiscover = false;

            // disable Datatables alerting behaviour
            if ($.fn.dataTableExt) { $.fn.dataTableExt.sErrMode = 'throw'; }

            testBrowser().done(function() {
                var modelRegistry = new ModelRegistry(this.getModelData());
                var pageData = this.getPageData();

                this.pages = new Pages({
                    defaultPage: this.defaultPage,
                    modelRegistry: modelRegistry,
                    pageData: pageData,
                    router: router
                });

                this.navigation = new this.Navigation({
                    pageData: pageData,
                    router: router
                });

                this.render();

                var matchedRoute = Backbone.history.start();

                if (!matchedRoute) {
                    vent.navigate(configuration().hasBiRole ? 'find/search/query/*' : 'find/search/splash');
                }
            }.bind(this));
        },

        render: function() {
            this.$el.html(this.template({
                username: configuration().username
            }));

            this.pages.setElement('.find-pages-container').render();

            this.navigation.render();

            this.$('.header').prepend(this.navigation.el);
        },

        // Can be overridden
        getModelData: function() {
            var modelData = {
                indexesCollection: {
                    Constructor: this.IndexesCollection
                },
                windowScrollModel: {
                    Constructor: WindowScrollModel,
                    fetch: false
                }
            };

            if (configuration().hasBiRole) {
                modelData.savedQueryCollection = {
                    Constructor: SavedQueryCollection,
                    fetchOptions: {remove: false}
                };
            }
            else {
                modelData.savedQueryCollection = {
                    Constructor: Backbone.Collection,
                    fetch: false
                };
            }

            return modelData;
        }
    });

});
