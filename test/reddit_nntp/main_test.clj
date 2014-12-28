(ns reddit-nntp.main-test
  (:require [clojure.test :refer :all]
            [reddit-nntp.main :refer :all]
            [clojure.data.json :as json]))

(defn stub-grab-posts-from-reddit []
  (json/write-str
   { "data"
     { "children"
       [
        { "data"
          { "id" "2qha"
            "title" "Foo"}}
        { "data"
          { "id" "3ab4"
            "title" "Bar"}}]}}))

(deftest test-extract-posts
  (let [expected #{ {"title" "Foo" "id" "2qha"},
                    {"title" "Bar" "id" "3ab4"}}
        input { "data" {
                         "children" [
                                       { "data"
                                         { "id" "2qha",
                                           "title" "Foo" }},
                                       { "data"
                                         { "id" "3ab4",
                                           "title" "Bar" }}]}}
        actual (extract-posts input)]
    (is (= expected actual))))

(deftest test-augment-post-with-comments
  (let [input-post { "id" "2qha"
                     "title" "Is AES broken?" }
        input-comments [ { "id" "foo"
                           "body" "No." }
                         { "id" "bar"
                           "body" "Yes" } ]
        expected { "id" "2qha"
                   "title" "Is AES broken?"
                   "children" [ { "id" "foo"
                                  "body" "No." }
                                { "id" "bar"
                                  "body" "Yes" } ] }
        actual (augment-post-with-comments input-comments input-post)]
    (is (= expected actual))))

(deftest test-reddit-nntp
  (let
      [expected
       [[{"id" "2qha" "title" "Foo" "references" []}
         {"id" "foo" "body" "This." "references" ["2qha"]}
         {"id" "foo.bar" "body" "Worst comment" "references" ["2qha" "foo"]}]
        [{"id" "3ab4" "title" "Bar" "references" []}
         {"id" "foo" "body" "This." "references" ["3ab4"]}
         {"id" "foo.bar" "body" "Worst comment" "references" ["3ab4" "foo"]}]]
       actual (reddit-nntp stub-grab-posts-from-reddit grab-comments-from-reddit)]
       
    (is (= expected actual))))

(deftest test-flatten-post-with-no-comments
  (let
      [input { "id" "foo" }
       expected [ { "id" "foo" } ]
       actual (flatten-post-and-comments input)]
    (is (= expected actual))))

(deftest test-flatten-post-with-non-nested-comments
  (let
      [input { "id" "foo"
               "children" [ { "id" "bar" }]}
       expected [ { "id" "foo" }
                  { "id" "bar" } ]
       actual (flatten-post-and-comments input)]
    (is (= expected actual))))

(deftest test-flatten-post-with-nested-coments
  (let
      [input { "id" "foo"
               "children" [ { "id" "bar"
                              "children" [ { "id" "baz" } ] }]}
       expected [{"id" "foo"}
                {"id" "bar"}
                {"id" "baz"}]
       actual (flatten-post-and-comments input)]
    (is (= expected actual))))

(deftest test-fill-in-references-from-parent-ids
  (let
      [input { "id" "foo"
               "children" [ { "id" "bar"
                              "children" [ { "id" "baz" } ] }]}
       expected { "id" "foo"
                  "references" []
                  "children" [ { "id" "bar"
                                 "references" [ "foo" ]
                                 "children" [ { "id" "baz"
                                                "children" []
                                                "references" [ "foo" "bar" ]}]}]}
       actual (fill-in-references-from-parent-ids input)]
    (is (= expected actual))))

