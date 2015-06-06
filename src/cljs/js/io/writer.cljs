(ns cljs.js.io.writer)

(defprotocol Writer
  (append! [_ data]
    "Appends the char or string to this writer")
  (flush! [_]
    "Flushes the stream")
  (write! [_ data] [_ data off len]
    "Writes data to stream, or optionally a portion of x"))
