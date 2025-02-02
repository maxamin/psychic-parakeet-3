import React from 'react';
import JSONPretty from 'react-json-pretty';
import 'react-json-pretty/themes/monikai.css';

export default function PackageParser(props) {
  return (
      <JSONPretty space="4" data={props.packageManifest}></JSONPretty>
  )
}