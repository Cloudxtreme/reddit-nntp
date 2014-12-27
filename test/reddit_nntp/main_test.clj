(ns reddit-nntp.main-test
  (:require [clojure.test :refer :all]
            [reddit-nntp.main :refer :all]))

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
                   "comments" [ { "id" "foo"
                                  "body" "No." }
                                { "id" "bar"
                                  "body" "Yes" } ] }
        actual (augment-post-with-comments input-comments input-post)]
    (is (= expected actual))))

(deftest test-reddit-nntp
  (let
      [ expected
         [{"comments"
            {"id" "foo",
             "body" "This.",
             "children" ["id" "foo.bar" "body" "Worst comment" "children" []]},
           "id" "2qha",
           "title" "Foo"}
          {"comments"
            {"id" "foo",
             "body" "This.",
             "children" ["id" "foo.bar" "body" "Worst comment" "children" []]},
           "id" "3ab4",
           "title" "Bar"}]
        actual (reddit-nntp stub-grab-posts-from-reddit grab-comments-from-reddit)]
    (is (= expected actual))))
