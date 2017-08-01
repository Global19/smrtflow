INSERT INTO am_subscriber VALUES (1, 'admin', -1234, '', '2017-07-28', 'admin', '2017-07-28', NULL, NULL);
SELECT pg_catalog.setval('am_subscriber_sequence', 1, true);

INSERT INTO am_application VALUES (1, 'DefaultApplication', 1, 'Unlimited', NULL, NULL, 'APPROVED', '', 'admin', '2017-07-28', NULL, NULL, '1cf8fa2f-538c-4ced-b895-7fa2177db615');
SELECT pg_catalog.setval('am_application_sequence', 1, true);

INSERT INTO am_application_registration VALUES (1, 1, '9e5d6255-e569-4788-a118-9cb8e8c31770', 1, 'PRODUCTION', 'default', '{"tokenScope":"default","validityPeriod":"3600","grant_types":"refresh_token,urn:ietf:params:oauth:grant-type:saml2-bearer,password,iwa:ntlm,client_credentials","key_type":"PRODUCTION","username":"admin"}', 'ALL', 0);
SELECT pg_catalog.setval('am_application_registration_sequence', 1, true);

INSERT INTO am_application_key_mapping VALUES (1, 'KMLz5g7fbmx8RVFKKdu0NOrJic4a', 'PRODUCTION', 'COMPLETED', 'CREATED');

INSERT INTO idn_oauth_consumer_apps VALUES (1, 'KMLz5g7fbmx8RVFKKdu0NOrJic4a', '6NjRXBcFfLZOwHc0Xlidiz4ywcsa', 'admin', -1234, 'PRIMARY', 'admin_DefaultApplication_PRODUCTION', 'OAuth-2.0', NULL, 'refresh_token urn:ietf:params:oauth:grant-type:saml2-bearer password iwa:ntlm client_credentials', '0', '0');
SELECT pg_catalog.setval('idn_oauth_consumer_apps_pk_seq', 1, true);
